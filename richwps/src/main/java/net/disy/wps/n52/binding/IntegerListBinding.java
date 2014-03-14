package net.disy.wps.n52.binding;

import org.n52.wps.io.data.IComplexData;

public class IntegerListBinding implements IComplexData {

    private static final long serialVersionUID = 2131395L;
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
        return this.payload.getClass();
    }

    @Override
    public void dispose() {
    }
}
