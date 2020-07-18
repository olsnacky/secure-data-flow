module com.qutifc.implementations.tokencollection.implementation {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.TokenCollection
			with com.qutifc.implementations.tokencollection.implementation.components.TokenCollectionImpl;
}