module com.qutifc.implementations.scanner.json {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Scanner
			with com.qutifc.implementations.scanner.json.components.JSONScanner;
}