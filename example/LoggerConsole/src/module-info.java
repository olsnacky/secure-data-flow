module com.qutifc.implementations.logger.console {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Logger
			with com.qutifc.implementations.logger.console.components.LoggerConsole;
}