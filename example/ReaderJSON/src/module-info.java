module com.qutifc.implementations.reader.json {
	requires com.qutifc.securitycontracts;
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Reader with com.qutifc.implementations.reader.json.components.JSONReader;
}