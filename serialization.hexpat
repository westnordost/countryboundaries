#pragma endian big
#pragma array_limit 2147483647

// This is a pattern definition file for ImHex.

struct utf8 {
    u16 length;
    char string[length];
};

struct geometrySize {
    utf8 id;
    double size;
};

struct point {
    u16 x;
    u16 y;
};

struct ring {
    s32 size;
    point points[size];
};

struct polygon {
    u8 size;
    ring rings[size];
};

struct areas {
    utf8 id;
    polygon outer;
    polygon inner;
};

struct cell {
    u8 containingIdsSize;
    utf8 containingIds[containingIdsSize];
    u8 intersectingAreasSize;
    areas intersectingAreas[intersectingAreasSize];
};


struct CountryBoundariesFile {
    u16 version;
    s32 geometryCount;
    geometrySize geometrySizes[geometryCount];
    s32 rasterWidth;
    s32 rasterSize;
    cell raster[rasterSize];
};

CountryBoundariesFile file @ 0x0;