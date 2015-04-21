package util;

public class MapUtil {

	/**
	 *  经纬度转墨卡托
	 * @param lon 经度
	 * @param lat 纬度
	 * @return
	 */
	public static double[] lonLat2Mercator(double lon, double lat) {
		double[] xy = new double[2];
		double x = lon * 20037508.342789 / 180;
		double y = Math.log(Math.tan((90 + lat) * Math.PI / 360))
				/ (Math.PI / 180);
		y = y * 20037508.34789 / 180;
		xy[0] = x;
		xy[1] = y;
		return xy;
	}

	/**
	 *  墨卡托转经纬度
	 * @param mercatorX
	 * @param mercatorY
	 * @return [0:经度,1:维度]
	 */
	public static double[] mercator2lonLat(double mercatorX, double mercatorY) {
		double[] xy = new double[2];
		//origin parameter:20037508.34
		double x = mercatorX / 20037740.34 * 180;
		double y = mercatorY / 19913740.34 * 180;
		y = 180 / Math.PI
				* (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
		xy[0] = x;
		xy[1] = y;
		return xy;
	}

	public static double[] UTMWGSXYtoBL(double Xn, double Yn) {
		double[] XYtoBL = new double[2];

		double Mf;
		double L0 = 105;// 中央经度（可以根据实际情况进行修改）
		double Nf;
		double Tf, Bf;
		double Cf;
		double Rf;
		double b1, b2, b3;
		double r1, r2;
		double K0 = 0.9996;
		double D, S;
		double FE = 500000;// 东纬偏移
		double FN = 0;
		double a = 6378137;
		double b = 6356752.3142;
		double e1, e2, e3;
		double B;
		double L;

		L0 = L0 * Math.PI / 180;// 弧度

		e1 = Math.sqrt(1 - Math.pow((b / a), 2.00));
		e2 = Math.sqrt(Math.pow((a / b), 2.00) - 1);
		e3 = (1 - b / a) / (1 + b / a);

		Mf = (Xn - FN) / K0;
		S = Mf
				/ (a * (1 - Math.pow(e1, 2.00) / 4 - 3 * Math.pow(e1, 4.00)
						/ 64 - 5 * Math.pow(e1, 6.00) / 256));

		b1 = (3 * e3 / 2.00 - 27 * Math.pow(e3, 3.00) / 32.00)
				* Math.sin(2.00 * S);
		b2 = (21 * Math.pow(e3, 2.00) / 16 - 55 * Math.pow(e3, 4.00) / 32)
				* Math.sin(4 * S);
		b3 = (151 * Math.pow(e3, 3.00) / 96) * Math.sin(6 * S);
		Bf = S + b1 + b2 + b3;

		Nf = (Math.pow(a, 2.00) / b)
				/ Math.sqrt(1 + Math.pow(e2, 2.00)
						* Math.pow(Math.cos(Bf), 2.00));
		r1 = a * (1 - Math.pow(e1, 2.00));
		r2 = Math.pow((1 - Math.pow(e1, 2.00) * Math.pow(Math.sin(Bf), 2.00)),
				3.0 / 2.0);
		Rf = r1 / r2;
		Tf = Math.pow(Math.tan(Bf), 2.00);
		Cf = Math.pow(e2, 2.00) * Math.pow(Math.cos(Bf), 2.00);
		D = (Yn - FE) / (K0 * Nf);

		b1 = Math.pow(D, 2.00) / 2.0;
		b2 = (5 + 3 * Tf + 10 * Cf - 4 * Math.pow(Cf, 2.0) - 9 * Math.pow(e2,
				2.0)) * Math.pow(D, 4.00) / 24;
		b3 = (61 + 90 * Tf + 298 * Cf + 45 * Math.pow(Tf, 2.00) - 252
				* Math.pow(e2, 2.0) - 3 * Math.pow(Cf, 2.0))
				* Math.pow(D, 6.00) / 720;
		B = Bf - Nf * Math.tan(Bf) / Rf * (b1 - b2 + b3);
		B = B * 180 / Math.PI;
		L = (L0 + (1 / Math.cos(Bf))
				* (D - (1 + 2 * Tf + Cf) * Math.pow(D, 3) / 6 + (5 + 28 * Tf
						- 2 * Cf - 3 * Math.pow(Cf, 2.0) + 8
						* Math.pow(e2, 2.0) + 24 * Math.pow(Tf, 2.0))
						* Math.pow(D, 5.00) / 120))
				* 180 / Math.PI;
		L0 = L0 * 180 / Math.PI;// 转化为度

		XYtoBL[0] = B;
		XYtoBL[1] = L;

		return XYtoBL;
	}

	public static void main(String[] args) {

		double[] num;
		System.out.println("---106.59968883591,29.65047829472-------------");
		num = mercator2lonLat(11866752.2, 3437369.1);
		for (int i = 0; i < num.length; i++) {
			System.out.println(num[i]);
		}
		
		System.out.println("---121.287085,24.880672-------------");
		num = mercator2lonLat(13501763.452386, 2842955.1396238);
		for (int i = 0; i < num.length; i++) {
			System.out.println(num[i]);
		}

		System.out.println("---120.88018799294,31.274647874293-------------");
		num = mercator2lonLat(13456467.39, 3646082.975);
		for (int i = 0; i < num.length; i++) {
			System.out.println(num[i]);
		}
		
		System.out.println("---118.80229595847,32.06743902904-------------");
//		num = mercator2lonLat(13225154.99, 3749272.06);
		num = mercator2lonLat(11866800, 3439060);
		for (int i = 0; i < num.length; i++) {
			System.out.println(num[i]);
		}
	}
}
