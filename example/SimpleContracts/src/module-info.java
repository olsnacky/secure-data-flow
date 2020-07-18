module com.qutifc.contracts.scanner {
	requires com.qutifc.simple.interfaces;

	provides com.qutifc.simple.interfaces.Greeter with com.qutifc.simple.contracts.GreeterSC;
}