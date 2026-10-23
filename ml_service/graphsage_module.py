"""
GraphSAGE Module for Online Payment Transaction Graph Learning (Optional Advanced Extension)

In financial transaction networks, fraudsters frequently operate in synthetic identity rings or mule chains:
Node Types:
- Sender Accounts (Originators: C1, C2...)
- Receiver Accounts (Destinations: C_mule, M_merchant...)
Edge:
- Directed Transaction with Amount, Timestamp, and Velocity attributes.

GraphSAGE (Hamilton et al.) computes inductive node representations by sampling and aggregating
features from a node's local neighborhood:
    h_{N(v)}^{(k)} = AGGREGATE_k({h_u^{(k-1)}, \forall u \in N(v)})
    h_v^{(k)} = \sigma(W^{(k)} \cdot CONCAT(h_v^{(k-1)}, h_{N(v)}^{(k)}))
"""

import math
from typing import Dict, List, Tuple

class SimpleGraphSAGE:
    def __init__(self, embedding_dim: int = 16):
        self.embedding_dim = embedding_dim
        # Adjacency list: sender -> list of (receiver, amount)
        self.adj_list: Dict[str, List[Tuple[str, float]]] = {}
        # Node initial features: [in_degree, out_degree, total_amount, avg_amount]
        self.node_features: Dict[str, List[float]] = {}

    def add_transaction(self, sender: str, receiver: str, amount: float):
        if sender not in self.adj_list:
            self.adj_list[sender] = []
        self.adj_list[sender].append((receiver, amount))

        # Update sender features
        if sender not in self.node_features:
            self.node_features[sender] = [0.0, 1.0, amount, amount]
        else:
            feat = self.node_features[sender]
            feat[1] += 1.0 # out-degree
            feat[2] += amount # total sent
            feat[3] = feat[2] / feat[1] # avg sent

        # Update receiver features
        if receiver not in self.node_features:
            self.node_features[receiver] = [1.0, 0.0, amount, amount]
        else:
            feat = self.node_features[receiver]
            feat[0] += 1.0 # in-degree
            feat[2] += amount # total received
            feat[3] = feat[2] / feat[0]

    def aggregate_neighborhood(self, node: str) -> List[float]:
        """Mean pooling aggregator over 1-hop destination neighborhood."""
        neighbors = self.adj_list.get(node, [])
        if not neighbors:
            return [0.0, 0.0, 0.0, 0.0]

        dim = 4
        pooled = [0.0] * dim
        for neighbor, _ in neighbors:
            feat = self.node_features.get(neighbor, [0.0]*dim)
            for d in range(dim):
                pooled[d] += feat[d]

        k = len(neighbors)
        return [p / k for p in pooled]

    def compute_embedding(self, node: str) -> List[float]:
        """Concatenates self-features and aggregated neighbor features."""
        self_feat = self.node_features.get(node, [0.0, 0.0, 0.0, 0.0])
        neigh_feat = self.aggregate_neighborhood(node)
        # Combined 8-dimensional graph inductive embedding
        combined = self_feat + neigh_feat
        # Apply non-linear activation (tanh)
        return [round(math.tanh(c / 1000.0), 4) for c in combined]

if __name__ == "__main__":
    sage = SimpleGraphSAGE()
    # Test transactions
    sage.add_transaction("C1001", "C9001", 50000.0)
    sage.add_transaction("C1002", "C9001", 45000.0)
    sage.add_transaction("C9001", "M5001", 95000.0)

    print("GraphSAGE Origin Embedding for C1001:", sage.compute_embedding("C1001"))
    print("GraphSAGE Mule Node Embedding for C9001:", sage.compute_embedding("C9001"))
