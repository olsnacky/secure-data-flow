package com.qutifc.securitycontracts.types;

import java.util.function.Consumer;

import com.qutifc.securitycontracts.securitylevels.SecurityLevel;

public class Channel<T extends SecurityLevel> {
	private SecurityLevel securityLevel;
	
	private String[] readValues;
	private int readIndex = 0;
	
	private Consumer<String> writeFunction;
	
	public Channel(String[] readValues) {
		this.securityLevel = securityLevel;
		this.readValues = readValues;
	}
	
	public Channel(Consumer<String> writeFunction) {
		this.securityLevel = securityLevel;
		this.writeFunction = writeFunction;
	}
	
	public SecurityLevel getSecurityLevel() {
		return this.securityLevel;
	}
	
	public String readFromChannel() {
		String retVal = this.readValues[this.readIndex];
		this.readIndex = this.readIndex + 1;
		return retVal;
	}
	
	public void writeToChannel(String value) {
		this.writeFunction.accept(value);
	}
}
