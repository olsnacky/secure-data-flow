package com.qutifc.securitycontracts.securitylevels;

public class Low implements SecurityLevel {
	public SecurityLevelType getLevel() {
		return SecurityLevelType.LOW;
	}
}
