/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.Value;

public class DFlipFlop extends AbstractFlipFlop {
	public DFlipFlop() {
		super("D Flip-Flop", "dFlipFlop.gif", Strings.getter("dFlipFlopComponent"), (byte) 1);
	}

	@Override
	protected Value computeValue(Value[] inputs, Value curValue) {
		return inputs[0];
	}

	@Override
	protected String getInputName(int index) {
		return "D";
	}
}
