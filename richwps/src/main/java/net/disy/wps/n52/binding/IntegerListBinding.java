package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;

public class IntegerListBinding implements IComplexData {

    private static final long serialVersionUID = 1L;
    protected transient net.disy.wps.lkn.mpa.types.IntegerList payload;

    public IntegerListBinding(net.disy.wps.lkn.mpa.types.IntegerList payload) {
        this.payload = payload;
    }

    @Override
    public net.disy.wps.lkn.mpa.types.IntegerList getPayload() {
        return this.payload;
    }

    @Override
    public Class<?> getSupportedClass() {
        return File.class;
    }

    @Override
    public void dispose() {
    }

}
