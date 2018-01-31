for i in `seq 1 1000`;
do
  dd if=/dev/zero of=output${i}.dat  bs=1M  count=10
done
