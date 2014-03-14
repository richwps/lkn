package net.disy.wps.n52.binding;

import java.io.IOException;
import org.n52.wps.io.data.IComplexData;
import net.disy.wps.lkn.mpa.types.IntegerList;

public class IntegerListBinding implements IComplexData {

    protected transient IntegerList payload;

    public IntegerListBinding(IntegerList payload) {
        this.payload = payload;
    }

    @Override
    public IntegerList getPayload() {
        return this.payload;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Class getSupportedClass() {
        return IntegerList.class;
    }

    public void dispose() {
    }
}
