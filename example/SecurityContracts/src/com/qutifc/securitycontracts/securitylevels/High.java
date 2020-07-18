package com.qutifc.securitycontracts.securitylevels;

public class High implements SecurityLevel {
	public SecurityLevelType getLevel() {
		return SecurityLevelType.HIGH;
	}
}
