module com.qutifc.clients {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	uses com.qutifc.interfaces.components.Reader;
	uses com.qutifc.interfaces.components.Writer;
	uses com.qutifc.interfaces.components.Logger;
	uses com.qutifc.interfaces.components.Record;
}