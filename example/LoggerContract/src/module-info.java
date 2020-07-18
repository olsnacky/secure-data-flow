module com.qutifc.contracts.logger {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Logger with com.qutifc.contracts.logger.components.LoggerContract;
}