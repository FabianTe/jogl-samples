#version 400 core

#define FRAG_COLOR		0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

uniform vec4 diffuse;

in Block
{
    vec3 color;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    color = vec4(inBlock.color, 1.0) * diffuse;
}

