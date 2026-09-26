"""
Time Complexity
Space Complexity
"""


def fractionToDecimalBrute(numerator: int, denominator: int) -> str:
    pass


"""
Time Complexity: O(n) - at most n unique remainders before a repeat
Space Complexity: O(n) -  dictionary holds at most n entries
"""


def fractionToDecimalTest(num: int, denom: int) -> str:
    # denom cannot be zero
    if denom == 0:
        raise ZeroDivisionError("cannot divide by zero")

    result = []
    remainder_map = {}

    # Edge case: zero numerator
    if num == 0:
        return "0"

    # --- Step 1: Handle sign ---
    # XOR: negative only if exactly one is negative
    # with OR, any negative input triggers the "-" sign so "-1/-2" will give "-0.5"
    if (num < 0) ^ (denom < 0):
        result.append("-")

    # case when both are negative
    num, denom = abs(num), abs(denom)

    # Step 2: Handle Integer part
    result.append(str(num // denom))
    remainder = num % denom

    # No decimal part needed
    if remainder == 0:
        return "".join(result)

    # Step 3: Handle decimal
    result.append(".")

    while remainder:
        # Cycle detected → insert parentheses at the stored index
        if remainder in remainder_map:
            # do something
            cycle_start = remainder_map[remainder]
            result.insert(cycle_start, "(")
            result.append(")")
            return "".join(result)

        # Record current position before appending the digit
        remainder_map[remainder] = len(result)

        # Long division step
        remainder *= 10
        result.append(str(remainder // denom))
        remainder %= denom

    return "".join(result)


# print(fractionToDecimal(-1, 0))
# print(fractionToDecimal(0, -2))
# print(fractionToDecimal(-2, -1))
# print(fractionToDecimal(-1, -2))
# print(fractionToDecimal(1, 3))
# print(fractionToDecimal(-1, -2))
# print(fractionToDecimal(-1, -8))


# TC: O(n), SC: O(n)
def fractionToDecimalTest(num: int, denom: int) -> str:
    """
    case 0: when denom is 0
    case 1: when num is 0
    # handle sign
    case 2: when one of num or demon is -ve
    case 3: when both num/denom are -ve
    # handle digit
    case 4: when there is no remainder
    # handle decimal
    case 5: when there is a remainder
    """

    result = []
    remainder_map = {}

    if not isinstance(num, int) or not isinstance(denom, int):
        return None

    if denom == 0:
        raise ZeroDivisionError("cannot divide by zero")

    if num == 0:
        return "0"

    # handle sign
    # when exactly one of the values are -ve
    if (num < 0) ^ (denom < 0):
        result.append("-")

    # here both are -ve
    num, denom = abs(num), abs(denom)

    # handle digits
    quotient = num // denom
    remainder = num % denom
    result.append(str(quotient))

    if remainder == 0:
        return "".join(result)

    # handle decimal (where there is a remainder)
    result.append(".")

    while remainder:
        if remainder in remainder_map:
            cycle_start = remainder_map[remainder]
            result.insert(cycle_start, "(")
            result.append(")")
            return "".join(result)

        remainder_map[remainder] = len(result)

        # long division
        remainder *= 10
        quotient = remainder // denom
        result.append(str(quotient))
        remainder %= denom

    return "".join(result)


print(fractionToDecimalTest(-1, 3))
