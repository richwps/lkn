<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor xmlns:xlink='http://www.w3.org/1999/xlink' xmlns:gml='http://www.opengis.net/gml' xmlns:ogc='http://www.opengis.net/ogc' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' version='1.0.0' xsi:schemaLocation='http://www.opengis.net/sld StyledLayerDescriptor.xsd' xmlns='http://www.opengis.net/sld' >
  <NamedLayer>
    <Name>WRRL_Makrophytenbewertung</Name>
    <UserStyle>
      <FeatureTypeStyle>
        <Rule>
          <Name>schlecht</Name>
          <Title>schlecht</Title>
          <ogc:Filter>
              <ogc:And>
                <ogc:PropertyIsLessThan>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.19</ogc:Literal>
                </ogc:PropertyIsLessThan>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.0</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
            </ogc:And>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill" >#ff0000</CssParameter>
              <CssParameter name="fill-opacity" >1</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke" >#ff0000</CssParameter>
              <CssParameter name="stroke-width" >0.4</CssParameter>
              <CssParameter name="stroke-opacity" >1</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule> 
        <Rule>
          <Name>unbefriedigend</Name>
          <Title>unbefriedigend</Title>
          <ogc:Filter>
              <ogc:And>
                <ogc:PropertyIsLessThan>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.39</ogc:Literal>
                </ogc:PropertyIsLessThan>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.2</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
            </ogc:And>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill" >#ffb90f</CssParameter>
              <CssParameter name="fill-opacity" >1</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke" >#ffb90f</CssParameter>
              <CssParameter name="stroke-width" >0.4</CssParameter>
              <CssParameter name="stroke-opacity" >1</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>     
        <Rule>
          <Name>m&#228;&#223;ig</Name>
          <Title>m&#228;&#223;ig</Title>
          <ogc:Filter>
              <ogc:And>
                <ogc:PropertyIsLessThan>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.59</ogc:Literal>
                </ogc:PropertyIsLessThan>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.4</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
            </ogc:And>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill" >#ffff00</CssParameter>
              <CssParameter name="fill-opacity" >1</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke" >#ffff00</CssParameter>
              <CssParameter name="stroke-width" >0.4</CssParameter>
              <CssParameter name="stroke-opacity" >1</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>   
        <Rule>
          <Name>gut</Name>
          <Title>gut</Title>
          <ogc:Filter>
              <ogc:And>
                <ogc:PropertyIsLessThan>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.79</ogc:Literal>
                </ogc:PropertyIsLessThan>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.6</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
            </ogc:And>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill" >#00cd00</CssParameter>
              <CssParameter name="fill-opacity" >1</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke" >#00cd00</CssParameter>
              <CssParameter name="stroke-width" >0.4</CssParameter>
              <CssParameter name="stroke-opacity" >1</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule> 
        <Rule>
          <Name>sehr gut</Name>
          <Title>sehr gut</Title>
          <ogc:Filter>
              <ogc:And>
                <ogc:PropertyIsLessThan>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>1.0</ogc:Literal>
                </ogc:PropertyIsLessThan>
                <ogc:PropertyIsGreaterThanOrEqualTo>
                  <ogc:PropertyName>MPBMeanEQR</ogc:PropertyName>
                  <ogc:Literal>0.8</ogc:Literal>
                </ogc:PropertyIsGreaterThanOrEqualTo>
              </ogc:And>
          </ogc:Filter>
          <PolygonSymbolizer>
            <Fill>
              <CssParameter name="fill" >#0000cd</CssParameter>
              <CssParameter name="fill-opacity" >1</CssParameter>
            </Fill>
            <Stroke>
              <CssParameter name="stroke" >#0000cd</CssParameter>
              <CssParameter name="stroke-width" >0.4</CssParameter>
              <CssParameter name="stroke-opacity" >1</CssParameter>
            </Stroke>
          </PolygonSymbolizer>
        </Rule>
      </FeatureTypeStyle>
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>