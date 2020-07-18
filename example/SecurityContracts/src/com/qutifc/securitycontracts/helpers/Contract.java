package com.qutifc.securitycontracts.helpers;

public class Contract {
	public static String phi(Object obj) {
		String retVal = "1";
		if (obj == null) {
			retVal = "2";
		}

		return retVal;
	}
}
