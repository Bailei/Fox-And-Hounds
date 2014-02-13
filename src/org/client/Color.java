package org.client;

public enum Color {
  F, S;

  public boolean isFox() {
    return this == F;
  }

  public boolean isSheep() {
    return this == S;
  }

  public Color getOppositeColor() {
    return this == F ? S : F;
  }
}
