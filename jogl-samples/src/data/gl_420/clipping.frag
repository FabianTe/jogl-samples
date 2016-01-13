#version 420 core

#define FRAG_COLOR	0

precision highp float;
precision highp int;
layout(std140, column_major) uniform;

layout(binding = 0) uniform sampler2D diffuse;

in Block
{
    vec4 position;
    vec2 texCoord;
} inBlock;

layout(location = FRAG_COLOR, index = 0) out vec4 color;

void main()
{
    if(inBlock.position.z > 16)
        discard;

    color = texture(diffuse, inBlock.texCoord.st);
}