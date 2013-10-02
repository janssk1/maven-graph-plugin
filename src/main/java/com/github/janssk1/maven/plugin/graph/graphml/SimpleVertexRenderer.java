package com.github.janssk1.maven.plugin.graph.graphml;

import java.awt.Color;

import com.github.janssk1.maven.plugin.graph.RenderOptions;
import com.github.janssk1.maven.plugin.graph.graph.Vertex;

/**
 * User: Luis Santos
 * Date: 15/08/13
 */
public class SimpleVertexRenderer implements RenderOptions.VertexRenderer
{

  private static final int K = 1024;

  private final ColorRange colorRange;

  private static ColorRange createColorRange()
  {
    Color[] colors = new Color[240];
    for (int i = 0; i < colors.length; i++)
    {
      colors[i] = new Color(colors.length - i, colors.length - i, 255);
    }
    return new FixedIntervalColorRange(0, 100000 * K, colors);

  }

  private boolean showVersion;

  public SimpleVertexRenderer()
  {
    this(createColorRange());
  }

  public SimpleVertexRenderer(ColorRange colorRange)
  {
    this.colorRange = colorRange;
  }

  public SimpleVertexRenderer(boolean showVersion)
  {
    this(createColorRange());
    this.showVersion = showVersion;
  }

  public RenderOptions.VertexInfo renderVertex(Vertex vertex)
  {
    long jarSize = vertex.getArtifact() != null ? vertex.getArtifact().getSize() : 0;
    Color color = colorRange.getColor(jarSize);
    String label = "";
    if (showVersion)
    {
      label = vertex.getArtifactIdentifier().toString();
    }
    else
    {
      label = vertex.getArtifactIdentifier().getIdentifierWithoutVersion();
    }
    return new RenderOptions.VertexInfo(label, color);
  }
}
