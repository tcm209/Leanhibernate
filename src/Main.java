import com.exp.MusicEntity;
import com.exp.TypeEntity;
import net.sf.ehcache.hibernate.HibernateUtil;
import org.hibernate.*;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {
    private static final SessionFactory ourSessionFactory;
    private static final ServiceRegistry serviceRegistry;

    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();

            serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
            ourSessionFactory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return ourSessionFactory.openSession();
    }

    public static void main(final String[] args) throws Exception {
        TestBF();
    }

    /*
    测试并发问题
     */
    public static void TestBF(){
        int size=5;
        ExecutorService exec = Executors.newCachedThreadPool();
        final Semaphore semp=new Semaphore(10);//模拟10个用户
        for (int index=0;index<50;index++){
            final int NO=index;
            Runnable run=new Runnable() {
                @Override
                public void run() {
                    try {
                        semp.acquire();//获取许可
                        System.out.println("Thread:" + NO);
                        updateForBF(NO);

                        System.out.println("第：" + NO + " 个");
                        semp.release();//释放
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            exec.execute(run);
        }
        exec.shutdown();

    }

    /*
    更新
     */
    public static void updateForBF(final int no){
        Session session=getSession();
        Transaction tr=session.beginTransaction();
        TypeEntity entity=new TypeEntity();
        entity.setId(20);
        entity.setName("this is "+no);
        session.saveOrUpdate(entity);
        System.out.print("第："+no+"\n");
        tr.commit();
    }

    /*
    测试异常
     */
    public static void TestDaodo(){
        TestDao dao=new TestDao();
        TypeEntity entity=dao.loadType(19);
        System.out.print("load"+entity.getName()+"\n");
    }

    /*
    load  get加载数据库内不存在对象时
     */
    public static void loadOrGetTest(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
//        TypeEntity entity=(TypeEntity) session.load(TypeEntity.class,30);
//        System.out.print("load加载数据库内不存在的id"+entity.getId()+"\n name"+entity.getName());

        TypeEntity typeEntity=(TypeEntity) session.get(TypeEntity.class,30);
        System.out.print("get加载"+typeEntity.getId()+"\n");



        transaction.commit();
    }
    /*
    get 加载：无论是否使用对象  get方法都会发送sql查
     */
    public static void TestGet(){

        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity entity=(TypeEntity) session.get(TypeEntity.class,22);
        System.out.print("get加载\n");

        transaction.commit();


    }

    /*
    load  获取ID没有发送sql
    user对象仅仅是一个保存了id的代理对象
    load 获取name  发送sql
     */
    public static void TestLoad(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity entity=(TypeEntity) session.load(TypeEntity.class,22);
        System.out.print("load加载 获取ID"+entity.getId()+"\n");

        System.out.print("load加载 获取NAME"+entity.getName()+"\n");


        transaction.commit();


    }

    /*
    saveOrUpdate 如果对象是持久化对象  缓存内对象有更改 执行更新  如果对象是瞬时对象  缓存内没有 执行save
     */
    public static void saveOrUpdTest(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();

//        TypeEntity typeEntity=new TypeEntity();
//        typeEntity.setId(20);
//        typeEntity.setName("测试持久化对象");
//        session.saveOrUpdate(typeEntity);
//        System.out.print("打印SQL\n");
        TypeEntity entity=new TypeEntity();

        entity.setName("youliduix ");
        session.saveOrUpdate(entity);
        System.out.print("打印SQL====2\n");

        transaction.commit();
    }

    /*
    持久化对象用delete方法后转变成一个瞬时对象 数据库中不存在  如果对此对象进行修改不会发出任何sql语句
     */
    public static void deleteTest(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity entity=new TypeEntity();
        entity.setId(21);
        session.delete(entity);
        entity.setName("new delete");


        transaction.commit();

    }


    /*
    瞬时状态转行持久状态 被session管理
     */
    public static void TestStatus(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity typeEntity=new TypeEntity();
        typeEntity.setName("USA");
        typeEntity.setRemark("FIRE");
        session.save(typeEntity);//被session管理
        System.out.print("瞬时状态装持久状态 持久状态NAME："+typeEntity.getName()+"\n");
        typeEntity.setName("new USA");

        System.out.print("新更新后的NAME："+typeEntity.getName()+"\n");

        transaction.commit();
    }

    /*
    session 内持久对象如果没发生改变 调用update不发送sql
     */
    public static void TestStatusOther(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity typeEntity=new TypeEntity();
        typeEntity.setName("USA");
        typeEntity.setRemark("FIRE");
        session.save(typeEntity);//被session管理
        System.out.print("瞬时状态装持久状态 持久状态NAME："+typeEntity.getName()+"\n");
        session.update(typeEntity);
        typeEntity.setName("newUSA");
        session.update(typeEntity);

        transaction.commit();
    }
    /*20181011 17.31
    游离对象转换成持久化对象
    离线对象，如果要使其变成持久化对象的话，我们不能使用save方法，而应该使用update方法
     */
    public static void TestStatusOther1(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity typeEntity=new TypeEntity();
        typeEntity.setId(20);
        typeEntity.setName("Insert");
        session.save(typeEntity);

        transaction.commit();

    }
    /*
    离线对象转换成持久对象  数据库内已存在数据
     */
    public static void TestStatusOther2(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity typeEntity=new TypeEntity();
        typeEntity.setId(20);
        typeEntity.setName("new INSERV");
        session.update(typeEntity);
        transaction.commit();
    }







    /*
    测试一对多关联
     */
    public static void TestSave(){
        Session session=getSession();
        Transaction transaction=session.beginTransaction();

        TypeEntity typeEntity=new TypeEntity();
        typeEntity.setName("CHINA");
        typeEntity.setRemark("SWEET");

        MusicEntity entity1=new MusicEntity();
        entity1.setName("Test001");
        entity1.setRemark("insert 001");
        entity1.setTypeEntity(typeEntity);

        MusicEntity entity2=new MusicEntity();
        entity2.setName("Test002");
        entity2.setRemark("insert 002");
        entity2.setTypeEntity(typeEntity);

        typeEntity.getMusics().add(entity1);
        typeEntity.getMusics().add(entity2);
        session.save(typeEntity);


        transaction.commit();
    }

    /*
    二级缓存缓存的仅仅是对象
     */
    public static void TestCacheEnrty(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity type=(TypeEntity) session.load(TypeEntity.class,10);
        System.out.print("对象检索 开启二级缓存："+type.getName()+"\n");
        transaction.commit();
        session.close();

        session=getSession();
        session.beginTransaction();
        TypeEntity entity=(TypeEntity) session.load(TypeEntity.class,10);
        System.out.print("对象检索 开启二级缓存："+entity.getName()+"\n");
        session.getTransaction().commit();
        session.close();
    }
    /*
    对象检索 查询时条件不同
     */
    public static void TestCacheEntry1(){
        Session session=null;
        session=getSession();
        Transaction transaction=session.beginTransaction();
        TypeEntity type=(TypeEntity) session.load(TypeEntity.class,10);
        System.out.print("对象检索 查询条件不同 第一次查询："+type.getName()+"\n");
        transaction.commit();
        session.close();

        session=getSession();
        session.beginTransaction();
        TypeEntity entity=(TypeEntity) session.load(TypeEntity.class,11);
        System.out.print("对象检索 查询条件不同 第二次查询："+entity.getName()+"\n");
        session.getTransaction().commit();
        session.close();
    }

    /*
    hql 缓存测试 <property name="hibernate.cache.use_query_cache">true</property>
     */
    public static void TestCache(){
        Session session=getSession();
        Transaction transaction=session.beginTransaction();
        List<TypeEntity> typeEntities=session.createQuery("from TypeEntity").setCacheable(true).list();
        for (TypeEntity entity:typeEntities){
            System.out.print("hql检索 第一次查询："+entity.getName()+"\n");
        }
        transaction.commit();
        session.close();

        Session session1=getSession();
        session1.beginTransaction();
        List<TypeEntity> typeEntitiesList=session1.createQuery("from TypeEntity").setCacheable(true).list();
        for(TypeEntity entity:typeEntitiesList){
            System.out.print("hql检索 第二次查询："+entity.getName()+"\n");
        }
        session1.getTransaction().commit();
        session1.close();

    }


}

class TestDao{


    @LazyCollection(LazyCollectionOption.FALSE)
    public TypeEntity loadType(int id){
        Session session=null;
        Transaction tx=null;
        TypeEntity entity=null;
        try {
            session= Main.getSession();
            tx=session.beginTransaction();
            entity=(TypeEntity)session.load(TypeEntity.class,id);
            tx.commit();

        }catch (Exception e){
            e.printStackTrace();
            tx.rollback();
        }finally {
            session.close();
        }
        return entity;
    }
}
