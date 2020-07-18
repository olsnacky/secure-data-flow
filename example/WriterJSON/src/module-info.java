module com.qutifc.implementations.writer.json {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Writer with com.qutifc.implementations.writer.json.components.WriterJSON;
}