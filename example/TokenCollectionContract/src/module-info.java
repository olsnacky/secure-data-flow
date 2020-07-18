module com.qutifc.contracts.tokencollection {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.TokenCollection
			with com.qutifc.contracts.tokencollection.components.TokenCollectionContract;
}