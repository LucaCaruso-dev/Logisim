/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.std.memory;

import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.StdAttr;

class ClockState implements Cloneable {
	private Value lastClock;

	public ClockState() {
		lastClock = Value.FALSE;
	}

	@Override
	public ClockState clone() {
		try {
			return (ClockState) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public boolean updateClock(Value newClock, Object trigger) {
		Value oldClock = lastClock;
		lastClock = newClock;
		if (trigger == null || trigger == StdAttr.TRIG_RISING) {		// Flip-Flop in rising edge
			return oldClock == Value.FALSE && newClock == Value.TRUE;
		} else if (trigger == StdAttr.TRIG_FALLING) {					// Flip-Flop in falling edge
			return oldClock == Value.TRUE && newClock == Value.FALSE;
		} else if (trigger == StdAttr.TRIG_HIGH) {						// Latch with enable in positive logic
			return newClock == Value.TRUE;
		} else if (trigger == StdAttr.TRIG_LOW) {						// Latch with enable in negative logic
			return newClock == Value.FALSE;
		} else {
			return oldClock == Value.FALSE && newClock == Value.TRUE;
		}
	}
}
