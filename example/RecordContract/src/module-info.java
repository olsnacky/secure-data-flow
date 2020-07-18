module com.qutifc.contract.record {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Record with com.qutifc.contracts.record.components.RecordContract;
}