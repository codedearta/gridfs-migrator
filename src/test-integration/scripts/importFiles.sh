for i in `seq 1 1000`;
do
  mongofiles put /vagrant_data/output${i}.dat
done
