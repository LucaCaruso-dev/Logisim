/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.cburch.logisim.analyze.model.AnalyzerModel;
import com.cburch.logisim.analyze.model.Entry;
import com.cburch.logisim.analyze.model.TruthTable;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.util.StringUtil;

public class Analyze {

	/*
	 * private static class ExpressionMap extends HashMap<Location, Expression> {
	 * 
	 * private static final long serialVersionUID = 1947923788805279096L; private
	 * Circuit circuit; private Set<Location> dirtyPoints = new HashSet<Location>();
	 * private Map<Location, Component> causes = new HashMap<Location, Component>();
	 * private Component currentCause = null;
	 * 
	 * ExpressionMap(Circuit circuit) { this.circuit = circuit; }
	 * 
	 * @Override public Expression put(Location point, Expression expression) {
	 * Expression ret = super.put(point, expression); if (currentCause != null)
	 * causes.put(point, currentCause); if (ret == null ? expression != null :
	 * !ret.equals(expression)) { dirtyPoints.add(point); } return ret; } }
	 * 
	 * //private static final int MAX_ITERATIONS = 100;
	 * 
	 * /** Checks whether any of the recently placed expressions in the expression
	 * map are self-referential; if so, return it.
	 */

	/*
	 * private static Expression checkForCircularExpressions(ExpressionMap
	 * expressionMap) throws AnalyzeException { for (Location point :
	 * expressionMap.dirtyPoints) { Expression expr = expressionMap.get(point); if
	 * (expr.isCircular()) return expr; } return null; }
	 */

	//
	// computeExpression
	//
	/**
	 * Computes the expression corresponding to the given circuit, or raises
	 * ComputeException if difficulties arise.
	 */

	/*
	 * public static void computeExpression(AnalyzerModel model, Circuit circuit,
	 * Map<Instance, String> pinNames) throws AnalyzeException { ExpressionMap
	 * expressionMap = new ExpressionMap(circuit);
	 * 
	 * ArrayList<String> inputNames = new ArrayList<String>(); ArrayList<String>
	 * outputNames = new ArrayList<String>(); ArrayList<Instance> outputPins = new
	 * ArrayList<Instance>(); for (Map.Entry<Instance, String> entry :
	 * pinNames.entrySet()) { Instance pin = entry.getKey(); String label =
	 * entry.getValue(); if (Pin.FACTORY.isInputPin(pin)) {
	 * expressionMap.currentCause = Instance.getComponentFor(pin); Expression e =
	 * Expressions.variable(label); expressionMap.put(pin.getLocation(), e);
	 * inputNames.add(label); } else { outputPins.add(pin); outputNames.add(label);
	 * } }
	 * 
	 * propagateComponents(expressionMap, circuit.getNonWires());
	 * 
	 * for (int iterations = 0; !expressionMap.dirtyPoints.isEmpty(); iterations++)
	 * { if (iterations > MAX_ITERATIONS) { throw new AnalyzeException.Circular(); }
	 * 
	 * propagateWires(expressionMap, new
	 * HashSet<Location>(expressionMap.dirtyPoints));
	 * 
	 * HashSet<Component> dirtyComponents = getDirtyComponents(circuit,
	 * expressionMap.dirtyPoints); expressionMap.dirtyPoints.clear();
	 * propagateComponents(expressionMap, dirtyComponents);
	 * 
	 * Expression expr = checkForCircularExpressions(expressionMap); if (expr !=
	 * null) throw new AnalyzeException.Circular(); }
	 * 
	 * model.setVariables(inputNames, outputNames); for (int i = 0; i <
	 * outputPins.size(); i++) { Instance pin = outputPins.get(i);
	 * model.getOutputExpressions().setExpression(outputNames.get(i),
	 * expressionMap.get(pin.getLocation())); } }
	 */

	//
	// ComputeTable
	//
	/** Returns a truth table corresponding to the circuit. */
	public static void computeTable(AnalyzerModel model, Project proj, Circuit circuit,
			Map<Instance, String> pinLabels) {
		ArrayList<Instance> inputPins = new ArrayList<Instance>();
		ArrayList<String> inputNames = new ArrayList<String>();
		ArrayList<Instance> outputPins = new ArrayList<Instance>();
		ArrayList<String> outputNames = new ArrayList<String>();
		for (Map.Entry<Instance, String> entry : pinLabels.entrySet()) {
			Instance pin = entry.getKey();
			if (Pin.FACTORY.isInputPin(pin)) {
				inputPins.add(pin);
				inputNames.add(entry.getValue());
			} else {
				outputPins.add(pin);
				outputNames.add(entry.getValue());
			}
		}

		int inputCount = inputPins.size();
		int rowCount = 1 << inputCount;
		Entry[][] columns = new Entry[outputPins.size()][rowCount];

		for (int i = 0; i < rowCount; i++) {
			CircuitState circuitState = new CircuitState(proj, circuit);
			for (int j = 0; j < inputCount; j++) {
				Instance pin = inputPins.get(j);
				InstanceState pinState = circuitState.getInstanceState(pin);
				boolean value = TruthTable.isInputSet(i, j, inputCount);
				Pin.FACTORY.setValue(pinState, value ? Value.TRUE : Value.FALSE);
			}

			Propagator prop = circuitState.getPropagator();
			prop.propagate();
			/*
			 * TODO for the SimulatorPrototype class do { prop.step(); } while
			 * (prop.isPending());
			 */
			// TODO: Search for circuit state

			if (prop.isOscillating()) {
				for (int j = 0; j < columns.length; j++) {
					columns[j][i] = Entry.OSCILLATE_ERROR;
				}
			} else {
				for (int j = 0; j < columns.length; j++) {
					Instance pin = outputPins.get(j);
					InstanceState pinState = circuitState.getInstanceState(pin);
					Entry out;
					Value outValue = Pin.FACTORY.getValue(pinState).get(0);
					if (outValue == Value.TRUE)
						out = Entry.ONE;
					else if (outValue == Value.FALSE)
						out = Entry.ZERO;
					else if (outValue == Value.ERROR)
						out = Entry.BUS_ERROR;
					else
						out = Entry.DONT_CARE;
					columns[j][i] = out;
				}
			}
		}

		model.setVariables(inputNames, outputNames);
		for (int i = 0; i < columns.length; i++) {
			model.getTruthTable().setOutputColumn(i, columns[i]);
		}
	}

	// computes outputs of affected components

	/*
	 * private static HashSet<Component> getDirtyComponents(Circuit circuit,
	 * Set<Location> pointsToProcess) throws AnalyzeException { HashSet<Component>
	 * dirtyComponents = new HashSet<Component>(); for (Location point :
	 * pointsToProcess) { for (Component comp : circuit.getNonWires(point)) {
	 * dirtyComponents.add(comp); } } return dirtyComponents; }
	 */

	//
	// getPinLabels
	//
	/**
	 * Returns a sorted map from Pin objects to String objects, listed in canonical
	 * order (top-down order, with ties broken left-right).
	 */
	public static SortedMap<Instance, String> getPinLabels(Circuit circuit) {
		Comparator<Instance> locOrder = new Comparator<Instance>() {
			@Override
			public int compare(Instance ac, Instance bc) {
				Location a = ac.getLocation();
				Location b = bc.getLocation();
				if (a.getY() < b.getY())
					return -1;
				if (a.getY() > b.getY())
					return 1;
				if (a.getX() < b.getX())
					return -1;
				if (a.getX() > b.getX())
					return 1;
				return a.hashCode() - b.hashCode();
			}
		};
		SortedMap<Instance, String> ret = new TreeMap<Instance, String>(locOrder);

		// Put the pins into the TreeMap, with null labels
		for (Instance pin : circuit.getAppearance().getPortOffsets(Direction.EAST).values()) {
			ret.put(pin, null);
		}

		// Process first the pins that the user has given labels.
		ArrayList<Instance> pinList = new ArrayList<Instance>(ret.keySet());
		HashSet<String> labelsTaken = new HashSet<String>();
		for (Instance pin : pinList) {
			String label = pin.getAttributeSet().getValue(StdAttr.LABEL);
			label = toValidLabel(label);
			if (label != null) {
				if (labelsTaken.contains(label)) {
					int i = 2;
					while (labelsTaken.contains(label + i))
						i++;
					label = label + i;
				}
				ret.put(pin, label);
				labelsTaken.add(label);
			}
		}

		// Now process the unlabeled pins.
		for (Instance pin : pinList) {
			if (ret.get(pin) != null)
				continue;

			String defaultList;
			if (Pin.FACTORY.isInputPin(pin)) {
				defaultList = Strings.get("defaultInputLabels");
				if (defaultList.indexOf(",") < 0) {
					defaultList = "a,b,c,d,e,f,g,h";
				}
			} else {
				defaultList = Strings.get("defaultOutputLabels");
				if (defaultList.indexOf(",") < 0) {
					defaultList = "x,y,z,u,v,w,s,t";
				}
			}

			String[] options = defaultList.split(",");
			String label = null;
			for (int i = 0; label == null && i < options.length; i++) {
				if (!labelsTaken.contains(options[i])) {
					label = options[i];
				}
			}
			if (label == null) {
				// This is an extreme measure that should never happen
				// if the default labels are defined properly and the
				// circuit doesn't exceed the maximum number of pins.
				int i = 1;
				do {
					i++;
					label = "x" + i;
				} while (labelsTaken.contains(label));
			}

			labelsTaken.add(label);
			ret.put(pin, label);
		}

		return ret;
	}

	/*
	 * private static void propagateComponents(ExpressionMap expressionMap,
	 * Collection<Component> components) throws AnalyzeException { for (Component
	 * comp : components) { ExpressionComputer computer = (ExpressionComputer)
	 * comp.getFeature(ExpressionComputer.class); if (computer != null) { try {
	 * expressionMap.currentCause = comp; computer.computeExpression(expressionMap);
	 * } catch (UnsupportedOperationException e) { throw new
	 * AnalyzeException.CannotHandle(comp.getFactory().getDisplayName()); } } else
	 * if (comp.getFactory() instanceof Pin || comp.getFactory() instanceof Text) {
	 * ; // pins are handled elsewhere } else { // pins are handled elsewhere throw
	 * new AnalyzeException.CannotHandle(comp.getFactory().getDisplayName()); } } }
	 */

	// propagates expressions down wires

	/*
	 * private static void propagateWires(ExpressionMap expressionMap,
	 * HashSet<Location> pointsToProcess) throws AnalyzeException {
	 * expressionMap.currentCause = null; for (Location p : pointsToProcess) {
	 * Expression e = expressionMap.get(p); expressionMap.currentCause =
	 * expressionMap.causes.get(p); WireBundle bundle =
	 * expressionMap.circuit.wires.getWireBundle(p); if (e != null && bundle != null
	 * && bundle.points != null) { for (Location p2 : bundle.points) { if
	 * (p2.equals(p)) continue; Expression old = expressionMap.get(p2); if (old !=
	 * null) { Component eCause = expressionMap.currentCause; Component oldCause =
	 * expressionMap.causes.get(p2); if (eCause != oldCause && !old.equals(e)) {
	 * throw new AnalyzeException.Conflict(); } } expressionMap.put(p2, e); } } } }
	 */

	// This function check label name
	private static String toValidLabel(String label) {
		StringBuilder buildLabel = new StringBuilder();	//StringBuilder for hold string in building
		for(int i = 0; i < label.length(); i++) {
			char c = label.charAt(i);
			int unicode = label.codePointAt(i);			// Get character unicode 
			if((unicode < 8192 || unicode > 8207) && (unicode < 8234 || unicode > 8239) && (unicode < 8298 || unicode > 8303)) { // Check that it is not a prohibited Unicode
				buildLabel.append(c);					// Add character on final string
			}
		}
		String newLabel = buildLabel.toString().trim();
		if(newLabel.length() > 0)						// If is find a character or number
			return newLabel;							// Return a final string
		else
			return null;
	}

	private Analyze() {
	}
}
