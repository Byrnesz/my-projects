﻿// Upgrade NOTE: replaced '_World2Object' with 'unity_WorldToObject'

Shader "Siki/05 diffuse fragment" {
	Properties {
		_Diffuse ("Diffuse Color", Color) = (1, 1, 1, 1)
	}
	SubShader {
		Pass {

			Tags { "LightMode" = "ForwardBase" }

CGPROGRAM
#include "Lighting.cginc" // 取得第一个直射光的颜色 _LightColor0, 第一个直射光的位置 _WorldSpaceLightPos0
// 定点函数 声明定点函数名
// 基本作用 完成定点坐标从模型空间到剪裁空间的转换 (从游戏环境转换到视野相机屏幕上)
#pragma vertex vert
// 片元函数 
// 基本作用 返回模型对应的屏幕上的每一个像素的颜色值
#pragma fragment frag

fixed3 _Diffuse;

// application to vertex
struct a2v {
	float4 vertex : POSITION; // 告诉Unity把模型空间下的顶点坐标填充给vertex
	float3 normal : NORMAL;
};

struct v2f {
	float4 position : SV_POSITION;
	fixed3 worldNormalDir : COLOR0;
};

v2f vert (a2v v) {	// 通过语义告诉系统，这个参数是干什么的，比如POSITION告诉系统是顶点坐标
													// SV_POSITION这个语义用来解释说明返回值，是剪裁空间下的顶点坐标
	v2f f;
	// UNITY_MATRIX_MVP 把一个坐标从模型空间转换到剪裁空间
	f.position = mul(UNITY_MATRIX_MVP, v.vertex);

	// _World2Object 把一个方向从世界空间转换到模型空间
	// 方向反一下，从模型空间到世界空间
	f.worldNormalDir = mul(v.normal, (float3x3)unity_WorldToObject);

	return f;
}

// 兰伯特光照模型
// Diffuse = 直射光颜色 * (cosΘ * 0.5 + 0.5)
// 半兰伯特光照模型
// Diffuse = 直射光颜色 * max (0, cosΘ(光和法线的夹角))

fixed4 frag (v2f f) : SV_Target {

	// UNITY_LIGHTMODEL_AMBIENT 用来获取环境光
	fixed3 ambient = UNITY_LIGHTMODEL_AMBIENT.rgb;

	fixed3 normalDir = normalize( f.worldNormalDir );

	// _WorldSpaceLightPos0 取得平行光位置
	fixed3 lightDir = normalize( _WorldSpaceLightPos0.xyz ); // 对于每个顶点来说，光的位置就是光的方向，因为是平行光

	// _LightColor0 取得平行光的颜色
	fixed3 diffuse = _LightColor0.rgb * max( dot(normalDir, lightDir), 0 ) * _Diffuse.rgb; // 取得漫反射颜色

	fixed3 tempColor = diffuse + ambient;

	return fixed4(tempColor, 1);
}

ENDCG
		}
	}
	Fallback "Diffuse"
}