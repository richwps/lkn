package net.disy.wps.n52.binding;

import java.io.File;
import net.disy.wps.lkn.mpa.types.ObservationFeatureCollectionList;

import org.n52.wps.io.data.IComplexData;

public class ObeservationFeatureCollectionListBinding implements IComplexData {

    private static final long serialVersionUID = 13154403015275324L;
    protected transient ObservationFeatureCollectionList payload;

    public ObeservationFeatureCollectionListBinding(ObservationFeatureCollectionList payload) {
        this.payload = payload;
    }

    @Override
    public ObservationFeatureCollectionList getPayload() {
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
