module com.qutifc.contracts.writer {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Writer with com.qutifc.contracts.writer.components.WriterContract;
}