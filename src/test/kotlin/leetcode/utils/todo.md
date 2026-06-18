# File to note improvements to leetcode framework.

1. Multiline string inputs like matrix. Worth cleaning/removing /n and whitespaces before parsing.

```kotlin
 "[[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]," +
        "[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]"

//VS

"""
 [[1,1,1,1,0,0,0,0],[1,1,1,1,0,0,0,0],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],
 [1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1],[1,1,1,1,1,1,1,1]]
 """
```

1. Result may be in different order than expected, consider add expectAnyOrder (or better name) to cases.
2. Current test failure message is not very helpful. Consider adding more information about the failure.
3. Validate type converted covered with tests.
