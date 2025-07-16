package io.pillopl.library.commons.aggregates;

import java.util.Objects;

public class Version {
    private final int version;

    public Version(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public static Version zero() {
        return new Version(0);
    }

    public Version next() {
        return new Version(this.version + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return version == version1.version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return "Version{" +
                "version=" + version +
                '}';
    }
}
