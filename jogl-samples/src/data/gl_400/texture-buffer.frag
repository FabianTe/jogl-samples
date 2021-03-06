#version 400

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform samplerBuffer diffuse;

in Block
{
    flat int instance;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = texelFetch(diffuse, inBlock.instance);
}
