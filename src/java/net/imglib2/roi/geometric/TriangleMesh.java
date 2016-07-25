package net.imglib2.roi.geometric;

import java.util.List;

import net.imglib2.AbstractRealInterval;
import net.imglib2.RealInterval;
import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessibleRealInterval;
import net.imglib2.realtransform.Translation3D;
import net.imglib2.roi.util.Contains;
import net.imglib2.roi.util.ContainsRealRandomAccess;
import net.imglib2.roi.util.IterableRandomAccessibleRegion;
import net.imglib2.roi.util.ROIUtils;
import net.imglib2.type.logic.BoolType;
import net.imglib2.util.LinAlgHelpers;

public class TriangleMesh extends AbstractRealInterval implements RealRandomAccessibleRealInterval< BoolType >, Contains< RealLocalizable > {

	private final List< ? extends RealLocalizable > vertices;
	private final List< int[] > facets;
	
	public TriangleMesh( final List< ? extends RealLocalizable > newVertices, final List< int[] > newFacets ) {
		super( ROIUtils.getBoundsReal( newVertices ) );
		assert( this.n == 3 );
		vertices = newVertices;
		facets = newFacets;
	}

	@Override
	public RealRandomAccess<BoolType> realRandomAccess() {
		return new ContainsRealRandomAccess( this );
	}

	@Override
	public RealRandomAccess<BoolType> realRandomAccess(RealInterval interval) {
		return realRandomAccess();
	}
	
	/**
	 * Get vertices defining the {@link Polygon}
	 * 
	 * @return {@link List} of {@link RealLocalizable}
	 */
	public List< ? extends RealLocalizable > getVertices()
	{
		return vertices;
	}	
	
	public RealLocalizable getVertex( int k ) {
		return vertices.get(k);
	}
	
	public List< int[] > getFacets() 
	{
		return facets;
	}
	
	/*public IterableRandomAccessibleRegion< BoolType > rasterize()
	{
		return new RasterizedTriangleMesh( this );
	}*/

	@Override
	public Contains<RealLocalizable> copyContains() {
		return this;
	}
	
	public static boolean rayIntersectsTriangle( RealLocalizable p, double[] d, RealLocalizable v0, RealLocalizable v1, RealLocalizable v2 )
	{
		//float e1[3],e2[3],h[3],s[3],q[3];
		double a,f,u,v;
		
		double v0f[] = new double[3], v1f[] = new double[3], v2f[] = new double[3],
			   e1f[] = new double[3], e2f[] = new double[3], hf[] = new double[3],
			   sf[] = new double[3], qf[] = new double[3], df[] = new double[3],
			   pf[] = new double[3];
		v0.localize(v0f);
		v1.localize(v1f);
		v2.localize(v2f);
		p.localize(pf);
		LinAlgHelpers.subtract( e1f, v1f, v0f );
		LinAlgHelpers.subtract( e2f, v2f, v0f );
			
		LinAlgHelpers.cross(hf, d, e2f);
				
		a = LinAlgHelpers.dot(e1f,hf);
	
		if (a > -0.00001 && a < 0.00001)
			return(false);
	
		f = 1/a;
		LinAlgHelpers.subtract( sf, pf, v0f );
		u = f * (LinAlgHelpers.dot(sf,hf));
	
		if (u < 0.0 || u > 1.0)
			return false;
	
		LinAlgHelpers.cross( qf, sf, e1f );
		v = f * LinAlgHelpers.dot(df,qf);
	
		if (v < 0.0 || u + v > 1.0)
			return false;
	
		double t = f * LinAlgHelpers.dot(e2f,qf);
	
		if (t > 0.00001)
			return true;	
		else 
			return false;
	
	}

	@Override
	public boolean contains(RealLocalizable l) {
		int crosses = 0;
		double[] d = new double[3];
		l.localize(d);
		for( int[] facet : facets ) {
			if( rayIntersectsTriangle( l, d, getVertex(facet[0]), getVertex(facet[1]), getVertex(facet[2]) ) ) {
				crosses++;
			}
		}		
		return ( crosses % 2 == 1 );
	}

}
