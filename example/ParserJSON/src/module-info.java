module com.qutifc.implementations.parser.json {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Parser with com.qutifc.implementations.parser.json.components.JSONParser;
}