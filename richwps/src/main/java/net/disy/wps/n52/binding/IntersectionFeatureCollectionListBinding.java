package net.disy.wps.n52.binding;

import java.io.File;

import org.n52.wps.io.data.IComplexData;
import net.disy.wps.lkn.mpa.types.IntersectionFeatureCollection;

public class IntersectionFeatureCollectionListBinding implements IComplexData {

    private static final long serialVersionUID = 13154403015275324L;
    protected transient IntersectionFeatureCollection payload;

    public IntersectionFeatureCollectionListBinding(IntersectionFeatureCollection payload) {
        this.payload = payload;
    }

    @Override
    public IntersectionFeatureCollection getPayload() {
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
