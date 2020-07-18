module com.qutifc.contracts.reader {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Reader with com.qutifc.contracts.reader.components.ReaderContract;
}