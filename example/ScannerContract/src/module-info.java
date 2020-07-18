module com.qutifc.contracts.scanner {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Scanner with com.qutifc.contracts.scanner.components.ScannerContract;
}