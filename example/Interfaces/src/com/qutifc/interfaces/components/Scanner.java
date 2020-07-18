package com.qutifc.interfaces.components;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName = "com.qutifc.contracts.scanner.components.ScannerContract")
public interface Scanner {
	TokenCollection getTokens();

	public void scan();

	void setText(String text);

	String type();

	public static Scanner get(String type) {
		Iterable<Scanner> scanners = ServiceLoader.load(Scanner.class);

		Iterator<Scanner> scannerIter = scanners.iterator();
		while (scannerIter.hasNext()) {
			Scanner scanner = scannerIter.next();
			if (scanner.type().equals(type)) {
				return scanner;
			}
		}

		return null;
	}
}
