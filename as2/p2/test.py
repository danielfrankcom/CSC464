from generals import run, Order

def get_totals(generals, result, order):

    totals = {"success": 0, "failure": 0}
    for i in range(len(result)):
        if not generals[i]: # general is loyal
            if result[i] == order:
                totals["success"] += 1
            else:
                totals["failure"] += 1

    return totals

def assert_success(generals, result, order):
    totals = get_totals(generals, result, order)

    assert totals["success"] == generals.count(False)
    assert totals["failure"] == 0

def assert_failure(generals, result, order):
    totals = get_totals(generals, result, order)

    assert totals["success"] != generals.count(False)
    assert totals["failure"] != 0


def test_recursion_levels(generals, assertion):

    for recursion in range(3):
        result = run(recursion, Order.attack, generals)
        assertion(generals, result, Order.attack)

    for recursion in range(3):
        result = run(recursion, Order.retreat, generals)
        assertion(generals, result, Order.retreat)


def main():

    for i in range(1, 6):
        generals = [False] * i
        test_recursion_levels(generals, assert_success)

    for i in range(1, 6):
        bad_generals = i
        good_generals = (3 * i) + 1
        generals = [False] * good_generals + [True] * bad_generals
        test_recursion_levels(generals, assert_success)

    print("All tests passed")


if __name__ == "__main__":
    main()
