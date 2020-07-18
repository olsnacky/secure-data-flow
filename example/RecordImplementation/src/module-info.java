module com.qutifc.implementations.record.implementation {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Record
			with com.qutifc.implementations.record.implementation.components.RecordImpl;
}