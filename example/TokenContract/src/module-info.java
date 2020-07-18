module com.qutifc.contracts.token {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Token with com.qutifc.contracts.token.components.TokenContract;
}