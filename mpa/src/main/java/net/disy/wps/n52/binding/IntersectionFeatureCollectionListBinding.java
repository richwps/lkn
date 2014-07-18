package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollectionList;

public class IntersectionFeatureCollectionListBinding implements IComplexData {

    private static final long serialVersionUID = 13164403015275334L;
    protected transient IntersectionFeatureCollectionList payload;

    public IntersectionFeatureCollectionListBinding(
            IntersectionFeatureCollectionList payload) {
        this.payload = payload;
    }

    @Override
    public IntersectionFeatureCollectionList getPayload() {
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
