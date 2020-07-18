module com.qutifc.imeplementations.token.implementation {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Token with com.qutifc.implementations.token.implementation.components.TokenImpl;
}