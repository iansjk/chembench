package edu.unc.ceccr.chembench.persistence;

import javax.persistence.*;

@Entity()
@Table(name = "cbench_randomForestParameters")
public class RandomForestParameters {
    private Long id;

    private String numTrees;
    private String descriptorsPerTree;
    private String minTerminalNodeSize;
    private String maxNumTerminalNodes;
    private int seed = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "numTrees")
    public String getNumTrees() {
        return numTrees;
    }

    public void setNumTrees(String numTrees) {
        this.numTrees = numTrees;
    }

    @Column(name = "minTerminalNodeSize")
    public String getMinTerminalNodeSize() {
        return minTerminalNodeSize;
    }

    public void setMinTerminalNodeSize(String minTerminalNodeSize) {
        this.minTerminalNodeSize = minTerminalNodeSize;
    }

    @Column(name = "descriptorsPerTree")
    public String getDescriptorsPerTree() {
        return descriptorsPerTree;
    }

    public void setDescriptorsPerTree(String descriptorsPerTree) {
        this.descriptorsPerTree = descriptorsPerTree;
    }

    @Column(name = "maxNumTerminalNodes")
    public String getMaxNumTerminalNodes() {
        return maxNumTerminalNodes;
    }

    public void setMaxNumTerminalNodes(String maxNumTerminalNodes) {
        this.maxNumTerminalNodes = maxNumTerminalNodes;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
}
