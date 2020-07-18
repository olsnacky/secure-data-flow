module com.qutifc.simple.implementations.greeter.starred {
	requires com.qutifc.simple.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.simple.interfaces.Greeter with com.qutifc.simple.implementations.greeter.StarredGreeter;
}