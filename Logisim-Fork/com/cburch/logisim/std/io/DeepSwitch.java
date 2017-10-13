package com.cburch.logisim.std.io;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.cburch.logisim.circuit.Wire;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstanceLogger;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstancePoker;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.std.io.Switch.Logger;
import com.cburch.logisim.std.io.Switch.Poker;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;

public class DeepSwitch extends InstanceFactory {
	public static class Logger extends InstanceLogger {
		@Override
		public String getLogName(InstanceState state, Object option) {
			return state.getAttributeValue(StdAttr.LABEL);
		}

		@Override
		public Value getLogValue(InstanceState state, Object option) {
			return state.getPort(0);
		}
	}
	
	public static class Poker extends InstancePoker {
		@Override
		public void mouseReleased(InstanceState state, MouseEvent e) {
			setValue(state, state.getPort(0).not());// opposite value
		}

		private void setValue(InstanceState state, Value val) {
			InstanceDataSingleton data = (InstanceDataSingleton) state.getData();
			if (data == null) {
				state.setData(new InstanceDataSingleton(val));
			} else {
				data.setValue(val);
			}
			state.getInstance().fireInvalidated();
		}
	}

	private static final Attribute<Integer> ATTR_NSWITCHES = Attributes.forIntegerRange("NSwitches",
			Strings.getter("NumberOfSwitch"), 1, 32);
	
	private static final int DEPTH = 3;
	public DeepSwitch() {
		super("DeepSwitch", Strings.getter("DeepSwitchComponent"));
		setAttributes(
				new Attribute[] { StdAttr.FACING, ATTR_NSWITCHES, Io.ATTR_COLOR, StdAttr.LABEL, Io.ATTR_LABEL_LOC,
						StdAttr.LABEL_FONT, Io.ATTR_LABEL_COLOR },
				new Object[] { Direction.EAST, 4, Color.WHITE, "", Direction.WEST, StdAttr.DEFAULT_LABEL_FONT,
						Color.BLACK });
		setFacingAttribute(StdAttr.FACING);
		setIconName("deepswitch.gif");
		setInstancePoker(Poker.class);
		setInstanceLogger(Logger.class);
	}
	
	private void computeTextField(Instance instance) {
		Direction facing = instance.getAttributeValue(StdAttr.FACING);
		Object labelLoc = instance.getAttributeValue(Io.ATTR_LABEL_LOC);

		Bounds bds = instance.getBounds();
		int x = bds.getX() + bds.getWidth() / 2;
		int y = bds.getY() + bds.getHeight() / 2;
		int halign = GraphicsUtil.H_CENTER;
		int valign = GraphicsUtil.V_CENTER;
		if (labelLoc == Io.LABEL_CENTER) {
			x = bds.getX() + (bds.getWidth() - DEPTH) / 2;
			y = bds.getY() + (bds.getHeight() - DEPTH) / 2;
		} else if (labelLoc == Direction.NORTH) {
			y = bds.getY() - 2;
			valign = GraphicsUtil.V_BOTTOM;
		} else if (labelLoc == Direction.SOUTH) {
			y = bds.getY() + bds.getHeight() + 2;
			valign = GraphicsUtil.V_TOP;
		} else if (labelLoc == Direction.EAST) {
			x = bds.getX() + bds.getWidth() + 2;
			halign = GraphicsUtil.H_LEFT;
		} else if (labelLoc == Direction.WEST) {
			x = bds.getX() - 2;
			halign = GraphicsUtil.H_RIGHT;
		}
		if (labelLoc == facing) {
			if (labelLoc == Direction.NORTH || labelLoc == Direction.SOUTH) {
				x += 2;
				halign = GraphicsUtil.H_LEFT;
			} else {
				y -= 2;
				valign = GraphicsUtil.V_BOTTOM;
			}
		}

		instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, x, y, halign, valign);
	}
	@Override
	protected void configureNewInstance(Instance instance) {
		instance.addAttributeListener();
		computeTextField(instance);
		updateports(instance);
	}
	@Override
	public Bounds getOffsetBounds(AttributeSet attrs) {
		Direction facing = attrs.getValue(StdAttr.FACING);
		int x =attrs.getValue(ATTR_NSWITCHES).intValue()*30+15;
		return Bounds.create(0,0,x,30).rotate(Direction.EAST, facing, 0, 0);
	}
	@Override
	protected void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
		if (attr == StdAttr.FACING || attr == ATTR_NSWITCHES) {
			instance.recomputeBounds();
			computeTextField(instance);
		} else if (attr == Io.ATTR_LABEL_LOC) {
			computeTextField(instance);
		}
	}
	@Override
	public void paintInstance(InstancePainter painter) {
		Bounds bds = painter.getBounds();
		int x = bds.getX();
		int y = bds.getY();
		int w = bds.getWidth();
		int h = bds.getHeight();

		Value val;
		if (painter.getShowState()) {
			InstanceDataSingleton data = (InstanceDataSingleton) painter.getData();
			val = data == null ? Value.FALSE : (Value) data.getValue();
		} else {
			val = Value.FALSE;
		}

		Color color = painter.getAttributeValue(Io.ATTR_COLOR);
		if (!painter.shouldDrawColor()) {
			int hue = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
			color = new Color(hue, hue, hue);
		}
		
		
	}

	@Override
	public void propagate(InstanceState state) {
		

	}
	private void updateports(Instance instance) {
		int switches = instance.getAttributeValue(ATTR_NSWITCHES).intValue();
		Port[] port = new Port[switches];
		for (int i = 0; i <= switches; i++) {
			port[i] = new Port(30*i, 0, Port.OUTPUT, 1);
		}
	}

}
