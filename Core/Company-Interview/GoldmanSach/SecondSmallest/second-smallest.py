def second_smallest(numbers):
    minimum = float("inf")
    second_minimum = float("inf")

    for number in numbers:
        # case 1: Found a new absolute minimum
        if minimum > number:
            second_minimum = minimum  # Old first is demoted to second
            minimum = number  # New num becomes first

        # case 2: Not smaller than first, but smaller than second
        elif number < second_minimum and number != minimum:
            second_minimum = number

    return minimum


print(
    second_smallest(
        [
            6,
            3,
            1,
            2,
        ]
    )
)
