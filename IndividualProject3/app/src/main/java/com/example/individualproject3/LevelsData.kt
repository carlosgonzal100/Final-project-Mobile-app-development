package com.example.individualproject3


//contains all data for the levels including the default
//data that i made in my level editor that have been hard coded
//into the app.
fun createAllLevels(): List<Level> {

    // -------- EASY 1 (your baked custom level) --------
    val easy1Tiles = listOf(
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "tl_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "inner_br", "floor", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty", "empty"),
        listOf("left_lower", "floor", "right_lower", "empty", "empty", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "br_lower", "empty", "empty", "empty", "empty", "empty", "empty")
    )

    val easyGame1 = gameMapFromTileIds(
        id = "easy level 1",
        startX = 1,
        startY = 7,
        goalX = 7,
        goalY = 1,
        tileIds = easy1Tiles
    )

    val easy2Tiles = listOf(
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "tr_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "top_upper", "top_upper", "top_upper", "tl_upper", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "right_lower", "empty", "empty", "empty", "empty"),
        listOf("bl_lower", "bottom_lower", "bottom_lower", "inner_tr", "floor", "right_lower", "empty", "empty", "empty", "empty"),
        listOf("empty", "empty", "empty", "left_lower", "floor", "inner_bl", "top_lower", "top_lower", "top_lower", "tr_lower"),
        listOf("empty", "empty", "empty", "left_lower", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "bl_lower", "bottom_lower", "bottom_lower", "bottom_lower", "inner_tr", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "empty", "left_lower", "floor", "right_lower"),
        listOf("empty", "empty", "empty", "empty", "empty", "empty", "empty", "bl_lower", "bottom_lower", "br_lower")
    )

    val easyGame2 = gameMapFromTileIds(
        id = "easy level 2",
        startX = 8,
        startY = 8,
        goalX = 8,
        goalY = 1,
        tileIds = easy2Tiles
    )


    val easy3Tiles = listOf(
        listOf("tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "empty", "top_lower", "top_lower", "tr_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "right_lower", "empty", "left_lower", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_wall", "inner_wall", "floor", "inner_bl", "top_lower", "inner_br", "floor", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "top_upper", "top_upper", "top_upper", "floor", "top_upper", "top_upper", "top_upper", "top_upper", "right_lower"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("left_lower", "floor", "inner_tl", "inner_tr", "floor", "inner_wall", "inner_wall", "inner_wall", "floor", "right_lower"),
        listOf("left_lower", "floor", "right_lower", "left_lower", "floor", "inner_wall", "inner_wall", "inner_wall", "floor", "right_lower"),
        listOf("left_lower", "floor", "right_lower", "left_lower", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("bl_lower", "bottom_lower", "br_lower", "bl_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower")
    )

    val easyGame3 = gameMapFromTileIds(
        id = "easy level 3",
        startX = 1,
        startY = 8,
        goalX = 8,
        goalY = 1,
        tileIds = easy3Tiles
    )

    val hard1Tiles = listOf(
        listOf("water", "water", "water", "water", "inner_wall", "water", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "inner_wall", "floor", "floor", "floor", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "water"),
        listOf("left_lower", "floor", "inner_wall", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "right_lower"),
        listOf("bl_lower", "bottom_lower", "left_lower", "floor", "floor", "floor", "inner_tl", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower"),
        listOf("water", "water", "left_lower", "floor", "floor", "floor", "right_lower", "water", "water", "water", "water", "water"),
        listOf("water", "tl_lower", "inner_br", "floor", "floor", "floor", "right_lower", "water", "water", "water", "water", "water"),
        listOf("tl_lower", "inner_br", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water"),
        listOf("left_lower", "floor", "floor", "floor", "inner_wall", "water", "water", "water", "water", "water", "water", "water"),
        listOf("water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water")
    )

    val hardGame1 = gameMapFromTileIds(
        id = "Hard level 1",
        startX = 2,
        startY = 10,
        goalX = 10,
        goalY = 4,
        tileIds = hard1Tiles
    )

    val hard2Tiles = listOf(
        listOf("water", "top_upper", "floor", "floor", "floor", "floor", "top_upper", "floor", "floor", "floor", "top_upper", "water"),
        listOf("water", "floor", "floor", "top_upper", "floor", "floor", "floor", "water", "top_upper", "floor", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "inner_wall", "floor", "floor", "inner_wall", "water", "water", "floor", "water"),
        listOf("water", "floor", "water", "water", "water", "water", "water", "water", "top_upper", "water", "floor", "water"),
        listOf("top_upper", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "top_upper"),
        listOf("water", "water", "water", "floor", "water", "inner_wall", "water", "inner_wall", "floor", "water", "water", "water"),
        listOf("water", "water", "water", "floor", "water", "inner_wall", "water", "inner_wall", "floor", "water", "water", "water"),
        listOf("water", "water", "water", "floor", "floor", "floor", "floor", "floor", "floor", "top_upper", "water", "water"),
        listOf("water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water", "water")
    )

    val hardGame2 = gameMapFromTileIds(
        id = "Hard level 2",
        startX = 3,
        startY = 10,
        goalX = 5,
        goalY = 5,
        tileIds = hard2Tiles
    )

    val hard3Tiles = listOf(
        listOf("tl_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "top_upper", "tr_upper"),
        listOf("left_upper", "tl_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "top_lower", "tr_lower", "right_upper"),
        listOf("left_upper", "left_lower", "water", "inner_wall", "water", "water", "water", "water", "water", "water", "water", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "inner_wall", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "inner_wall", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "inner_wall", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "inner_wall", "floor", "floor", "inner_wall", "floor", "floor", "floor", "inner_wall", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "inner_wall", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "floor", "floor", "floor", "floor", "inner_wall", "floor", "floor", "floor", "floor", "water", "right_lower", "right_upper"),
        listOf("left_upper", "left_lower", "inner_wall", "water", "water", "water", "water", "water", "water", "water", "inner_wall", "water", "right_lower", "right_upper"),
        listOf("left_upper", "bl_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "bottom_lower", "br_lower", "right_upper"),
        listOf("bl_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "bottom_upper", "br_upper")
    )

    val hardGame3 = gameMapFromTileIds(
        id = "Hard level 3",
        startX = 6,
        startY = 7,
        goalX = 7,
        goalY = 4,
        tileIds = hard3Tiles
    )

    // -------- EASY LEVEL COLLECTION --------
    val easyLevel = Level(
        id = "easy_level",
        name = "Easy Dungeons",
        difficulty = Difficulty.EASY,
        games = listOf(easyGame1,easyGame2,easyGame3)
    )

    // -------- HARD LEVEL (placeholder for now) --------
    val hardLevel = Level(
        id = "hard_level",
        name = "Hard Dungeons",
        difficulty = Difficulty.HARD,
        games = listOf(hardGame1, hardGame2,hardGame3)   // later you can add baked hardGame1, hardGame2, etc.
    )

    return listOf(easyLevel, hardLevel)
}