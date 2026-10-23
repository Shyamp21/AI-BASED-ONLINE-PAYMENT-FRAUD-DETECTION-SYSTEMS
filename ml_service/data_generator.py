"""
Data generator for PaySim-compatible synthetic mobile payment transactions.
Generates realistic financial transaction streams with legitimate behavior and
known fraudulent patterns (account drain, phantom deposits, mule cash-outs).
"""

import csv
import os
import random
import time

def generate_paysim_dataset(num_records=5000, fraud_ratio=0.08, output_path="data/sample_transactions.csv"):
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    random.seed(42)

    headers = [
        "step", "type", "amount", "nameOrig", "oldbalanceOrg",
        "newbalanceOrig", "nameDest", "oldbalanceDest", "newbalanceDest",
        "isFraud", "isFlaggedFraud"
    ]

    types_legit = ["PAYMENT", "PAYMENT", "PAYMENT", "CASH_IN", "CASH_OUT", "TRANSFER", "DEBIT"]
    rows = []

    num_fraud = int(num_records * fraud_ratio)
    num_legit = num_records - num_fraud

    # 1. Generate Legitimate Transactions
    for i in range(num_legit):
        step = random.randint(1, 744) # 1 month hourly steps
        txn_type = random.choice(types_legit)
        name_orig = f"C{random.randint(1000000000, 9999999999)}"

        if txn_type == "PAYMENT":
            name_dest = f"M{random.randint(1000000000, 9999999999)}"
            old_orig = round(random.uniform(50, 50000), 2)
            amount = round(random.uniform(1, min(old_orig, 2000)), 2)
            new_orig = round(old_orig - amount, 2)
            old_dest = 0.0 # Merchant balances often tracked as 0 in PaySim
            new_dest = 0.0
        elif txn_type == "CASH_IN":
            name_dest = f"C{random.randint(1000000000, 9999999999)}"
            amount = round(random.uniform(100, 25000), 2)
            old_orig = round(random.uniform(0, 50000), 2)
            new_orig = round(old_orig + amount, 2)
            old_dest = round(random.uniform(100, 50000), 2)
            new_dest = round(old_dest, 2)
        elif txn_type == "CASH_OUT":
            name_dest = f"M{random.randint(1000000000, 9999999999)}"
            old_orig = round(random.uniform(500, 80000), 2)
            amount = round(random.uniform(100, min(old_orig, 15000)), 2)
            new_orig = round(old_orig - amount, 2)
            old_dest = round(random.uniform(100, 100000), 2)
            new_dest = round(old_dest + amount, 2)
        elif txn_type == "TRANSFER":
            name_dest = f"C{random.randint(1000000000, 9999999999)}"
            old_orig = round(random.uniform(1000, 100000), 2)
            amount = round(random.uniform(100, min(old_orig, 25000)), 2)
            new_orig = round(old_orig - amount, 2)
            old_dest = round(random.uniform(0, 50000), 2)
            new_dest = round(old_dest + amount, 2)
        else: # DEBIT
            name_dest = f"C{random.randint(1000000000, 9999999999)}"
            old_orig = round(random.uniform(500, 10000), 2)
            amount = round(random.uniform(10, min(old_orig, 1500)), 2)
            new_orig = round(old_orig - amount, 2)
            old_dest = round(random.uniform(1000, 20000), 2)
            new_dest = round(old_dest + amount, 2)

        rows.append([
            step, txn_type, amount, name_orig, old_orig,
            new_orig, name_dest, old_dest, new_dest, 0, 0
        ])

    # 2. Generate Fraudulent Transactions (Patterns: Account Draining, Zero Dest Balance Anomaly)
    for i in range(num_fraud):
        step = random.randint(1, 744)
        fraud_type = random.choice(["TRANSFER", "CASH_OUT"])
        name_orig = f"C{random.randint(1000000000, 9999999999)}"
        name_dest = f"C{random.randint(1000000000, 9999999999)}"

        # Fraud pattern 1: Total account drain
        old_orig = round(random.uniform(10000, 1500000), 2)
        amount = old_orig # Drains exact balance
        new_orig = 0.0

        if fraud_type == "TRANSFER":
            # Fraud TRANSFER typically to mule account that starts with 0
            old_dest = 0.0
            new_dest = 0.0 # Anomaly: money disappeared or uncredited
        else: # CASH_OUT
            old_dest = round(random.uniform(0, 5000), 2)
            new_dest = round(old_dest + amount, 2)

        is_flagged = 1 if amount > 200000 else 0

        rows.append([
            step, fraud_type, amount, name_orig, old_orig,
            new_orig, name_dest, old_dest, new_dest, 1, is_flagged
        ])

    # Shuffle rows
    random.shuffle(rows)

    with open(output_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        writer.writerows(rows)

    print(f"Generated {len(rows)} transactions ({num_fraud} fraud, {num_legit} legit) at {output_path}")

    # Also generate a smaller batch input sample for user CSV upload demo
    batch_path = os.path.join(os.path.dirname(output_path), "sample_batch_input.csv")
    with open(batch_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(headers[:9]) # Without target columns
        for r in rows[:25]:
            writer.writerow(r[:9])
    print(f"Generated sample batch input file at {batch_path}")

if __name__ == "__main__":
    current_dir = os.path.dirname(os.path.abspath(__file__))
    output_file = os.path.join(current_dir, "data", "sample_transactions.csv")
    generate_paysim_dataset(num_records=3000, fraud_ratio=0.10, output_path=output_file)
