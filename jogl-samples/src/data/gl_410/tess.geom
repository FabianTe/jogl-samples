#version 410 core

#define POSITION		0
#define COLOR			3
#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;
layout(triangles, invocations = 1) in;
layout(triangle_strip, max_vertices = 4) out;

in gl_PerVertex
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
} gl_in[];

struct Vertex
{
    vec4 color;
};

layout(location = 0) in Vertex inVertex[];

out gl_PerVertex 
{
    vec4 gl_Position;
    float gl_PointSize;
    float gl_ClipDistance[];
};

layout(location = 0) out Vertex outVertex;

void main()
{	
    for(int i = 0; i < gl_in.length(); ++i)
    {
        gl_Position = gl_in[i].gl_Position;
        outVertex.color = inVertex[i].color;
        EmitVertex();
    }
    EndPrimitive();
}

